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
        parsers.add(new Parser("item", "parseable/item.xml"));   // for now no reverse search

    }

    public Index populatePathIndex() {
        Index index = new Index();
        for (Parser parser : parsers) {
            index.update(parser.populatePathIndex());
        }
        return index;
    }

//    public Index populateParameterIndex(Index index) {
//        Index index = new Index();
//        for (Parser parser : parsers) {
//            index.update(parser.populateParameterIndex(index));
//        }
//        return index;
//    }

}
