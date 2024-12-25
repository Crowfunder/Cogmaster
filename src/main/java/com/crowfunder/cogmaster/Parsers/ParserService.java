package com.crowfunder.cogmaster.Parsers;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParserService {
    private List<Parser> parsers;

    public ParserService() {

        // Initalize existing parsers, probably not the best idea to do it like that but oh well
        parsers = new ArrayList<>();

        parsers.add(new Parser());
    }
}
