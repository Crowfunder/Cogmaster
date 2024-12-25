package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.Index.Index;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParserService {
    private final List<Parser> parsers;

    public ParserService() {

        // Initialize existing parsers, probably not the best idea to do it like that but oh well
        parsers = new ArrayList<>();
        parsers.add(new Parser("parseable/item.xml"));   // for now no reverse search

        parse();
    }

    public Index parse() {
        Index index = new Index();
        for (Parser parser : parsers) {
            index.update(parser.parse());
        }
        return index;
    }
}
