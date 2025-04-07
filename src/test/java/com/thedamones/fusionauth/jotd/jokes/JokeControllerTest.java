package com.thedamones.fusionauth.jotd.jokes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thedamones.fusionauth.jotd.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static com.thedamones.fusionauth.jotd.jokes.TestJokes.TEST_DATE;
import static com.thedamones.fusionauth.jotd.jokes.TestJokes.TEST_DESCRIPTION;
import static com.thedamones.fusionauth.jotd.jokes.TestJokes.TEST_ID;
import static com.thedamones.fusionauth.jotd.jokes.TestJokes.TEST_JOKE;
import static com.thedamones.fusionauth.jotd.jokes.TestJokes.createTestCreateJokeRecord;
import static com.thedamones.fusionauth.jotd.jokes.TestJokes.createTestJokeRecord;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JokeController.class)
@Import({JokeModelAssembler.class, SecurityConfig.class})
class JokeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JokeRepository jokeRepository;

    @MockitoBean
    private JokeService jokeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getJokes_shouldReturnPageOfJokes() throws Exception {
        Pageable pageRequest = PageRequest.ofSize(20);
        Page<JokeRecord> page = TestJokes.createPageOfJokeRecords(pageRequest, 100);
        JokeRecord joke0 = page.getContent().getFirst();
        when(jokeService.getJokes(null, pageRequest)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$._embedded.jokes[0].id", is(joke0.id().toString())))
                .andExpect(jsonPath("$._embedded.jokes[0].date", is(joke0.date().toString())))
                .andExpect(jsonPath("$._embedded.jokes[0].joke", is(joke0.joke())))
                .andExpect(jsonPath("$._embedded.jokes[0].description", is(joke0.description())))
                .andExpect(jsonPath("$.page.number", is(pageRequest.getPageNumber())))
                .andExpect(jsonPath("$.page.size", is(pageRequest.getPageSize())))
                .andExpect(jsonPath("$.page.totalElements", equalTo((int) page.getTotalElements())));
    }

    @Test
    @WithMockUser
    void getJokes_withDate() throws Exception {
        Pageable pageRequest = PageRequest.ofSize(20);
        Page<JokeRecord> page = TestJokes.createPageOfJokeRecords(pageRequest, 100);
        JokeRecord joke0 = page.getContent().getFirst();
        when(jokeService.getJokes(joke0.date(), pageRequest)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes?date=" + joke0.date().toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$._embedded.jokes[0].id", is(joke0.id().toString())))
                .andExpect(jsonPath("$._embedded.jokes[0].date", is(joke0.date().toString())))
                .andExpect(jsonPath("$._embedded.jokes[0].joke", is(joke0.joke())))
                .andExpect(jsonPath("$._embedded.jokes[0].description", is(joke0.description())))
                .andExpect(jsonPath("$.page.number", is(pageRequest.getPageNumber())))
                .andExpect(jsonPath("$.page.size", is(pageRequest.getPageSize())))
                .andExpect(jsonPath("$.page.totalElements", equalTo((int) page.getTotalElements())));
    }

    @Test
    public void getJokes_withoutUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createJoke_shouldCreateNewResource() throws Exception {
        CreateJokeRecord request = createTestCreateJokeRecord();
        JokeRecord jokeRecord = createTestJokeRecord();
        String selfLink = "/api/v1/jokes/" + jokeRecord.id().toString();
        when(jokeService.addJoke(request)).thenReturn(jokeRecord);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/jokes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(header().string(HttpHeaders.LOCATION, endsWith(selfLink)))
                .andExpect(jsonPath("$.id", is(jokeRecord.id().toString())))
                .andExpect(jsonPath("$.date", is(jokeRecord.date().toString())))
                .andExpect(jsonPath("$.joke", is(jokeRecord.joke())))
                .andExpect(jsonPath("$.description", is(jokeRecord.description())))
                .andExpect(jsonPath("$._links.self.href", endsWith(selfLink)));
    }

    @Test
    @WithMockUser
    void createJoke_whenDuplicateDate() throws Exception {
        CreateJokeRecord request = createTestCreateJokeRecord();
        when(jokeService.addJoke(request)).thenThrow(JokeDataIntegrityException.class);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/jokes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void createJoke_whenNullDate() throws Exception {
        CreateJokeRecord request = new CreateJokeRecord(null, TEST_JOKE, TEST_DESCRIPTION);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/jokes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createJoke_withoutUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/jokes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createJoke_whenBlankJoke() throws Exception {
        CreateJokeRecord request = new CreateJokeRecord(TEST_DATE, "", TEST_DESCRIPTION);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/jokes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getJokeOfTheDay_shouldReturnResourceModel() throws Exception {
        JokeRecord jokeRecord = createTestJokeRecord();
        String selfLink = "/api/v1/jokes/" + jokeRecord.id().toString();
        when(jokeService.getJokeOfTheDay()).thenReturn(jokeRecord);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes/today"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(jokeRecord.id().toString())))
                .andExpect(jsonPath("$.date", is(jokeRecord.date().toString())))
                .andExpect(jsonPath("$.joke", is(jokeRecord.joke())))
                .andExpect(jsonPath("$.description", is(jokeRecord.description())))
                .andExpect(jsonPath("$._links.self.href", endsWith(selfLink)));
    }

    @Test
    void getJokeOfTheDay_whenNotFound() throws Exception {
        when(jokeService.getJokeOfTheDay()).thenThrow(NoJokeOfTheDayException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes/today"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getJoke_shouldReturnResourceModel() throws Exception {
        JokeRecord jokeRecord = createTestJokeRecord();
        String selfLink = "/api/v1/jokes/" + jokeRecord.id().toString();
        when(jokeService.getJoke(jokeRecord.id())).thenReturn(jokeRecord);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes/" + jokeRecord.id()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(jokeRecord.id().toString())))
                .andExpect(jsonPath("$.date", is(jokeRecord.date().toString())))
                .andExpect(jsonPath("$.joke", is(jokeRecord.joke())))
                .andExpect(jsonPath("$.description", is(jokeRecord.description())))
                .andExpect(jsonPath("$._links.self.href", endsWith(selfLink)));
    }

    @Test
    @WithMockUser
    void getJoke_whenNotFound() throws Exception {
        when(jokeService.getJoke(TEST_ID)).thenThrow(JokeNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/jokes/" + TEST_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateJoke_shouldSaveJokeUpdates() throws Exception {
        JokeRecord jokeRecord = createTestJokeRecord();
        String selfLink = "/api/v1/jokes/" + jokeRecord.id().toString();
        when(jokeService.getJoke(jokeRecord.id())).thenReturn(jokeRecord);
        when(jokeService.updateJoke(jokeRecord.id(), jokeRecord)).thenReturn(jokeRecord);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/jokes/" + jokeRecord.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jokeRecord)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.id", is(jokeRecord.id().toString())))
                .andExpect(jsonPath("$.date", is(jokeRecord.date().toString())))
                .andExpect(jsonPath("$.joke", is(jokeRecord.joke())))
                .andExpect(jsonPath("$.description", is(jokeRecord.description())))
                .andExpect(jsonPath("$._links.self.href", endsWith(selfLink)));
    }

    @Test
    @WithMockUser
    void updateJoke_withChangedId() throws Exception {
        UUID jokeId = UUID.randomUUID();
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeService.updateJoke(jokeId, jokeRecord)).thenThrow(JokeDataOperationException.class);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/jokes/" + jokeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jokeRecord)))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void updateJoke_whenNotFound() throws Exception {
        JokeRecord jokeRecord = createTestJokeRecord();
        when(jokeService.updateJoke(jokeRecord.id(), jokeRecord)).thenThrow(JokeNotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/jokes/" + jokeRecord.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jokeRecord)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(NOT_FOUND.value())))
                .andExpect(jsonPath("$.title", is(NOT_FOUND.getReasonPhrase())));
    }

    @Test
    @WithMockUser
    void deleteJoke_shouldRemoveJoke() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/jokes/" + TEST_ID))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(jokeService).removeJoke(TEST_ID);
    }

    @Test
    @WithMockUser
    void deleteJoke_whenNotFound() throws Exception {
        doThrow(JokeNotFoundException.class).when(jokeService).removeJoke(TEST_ID);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/jokes/" + TEST_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}