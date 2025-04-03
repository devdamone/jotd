package com.thedamones.fusionauth.jotd.demo;

import com.thedamones.fusionauth.jotd.jokes.CreateJokeRecord;
import com.thedamones.fusionauth.jotd.jokes.JokeRecord;
import com.thedamones.fusionauth.jotd.jokes.JokeService;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/jotd")
@Profile("demo")
public class JokeOfTheDayController {

    private final JokeService jokeService;
    private final CsvParserService csvParserService;

    public JokeOfTheDayController(JokeService jokeService, CsvParserService csvParserService) {
        this.jokeService = jokeService;
        this.csvParserService = csvParserService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<String> uploadJokesFromCsv(@RequestParam("csv") @NotNull MultipartFile file) {
        List<CreateJokeRecord> jokes = csvParserService.parseRecords(file, CreateJokeRecord.class);
        jokes.forEach(jokeService::addJoke);
        return ResponseEntity.ok("Jokes uploaded successfully.");
    }

    @GetMapping
    public String jotd(Model model) {
        JokeRecord joke = jokeService.getJokeOfTheDay();
        model.addAttribute("date", LocalDate.now());
        model.addAttribute("joke", joke);
        return "jotd";
    }

    @GetMapping("/{date}")
    public String jotd(@PathVariable LocalDate date, Model model) {
        Page<JokeRecord> jokes = jokeService.getJokes(date, PageRequest.ofSize(1));
        JokeRecord joke = jokes.stream().filter(j -> date.equals(j.date())).findFirst().orElse(null);
        model.addAttribute("date", date);
        model.addAttribute("joke", joke);
        return "jotd";
    }
}
