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
        parsers.add(new Parser("item", "src/main/resources/parseable/item.xml"));   // for now no reverse search
        parsers.add(new Parser("actor", "src/main/resources/parseable/actor.xml"));   // for now no reverse search
        parsers.add(new Parser("attack", "src/main/resources/parseable/attack.xml"));   // for now no reverse search
        parsers.add(new Parser("accessory", "src/main/resources/parseable/accessory.xml"));   // for now no reverse search
        parsers.add(new Parser("conversation", "src/main/resources/parseable/conversation.xml"));   // for now no reverse search
        parsers.add(new Parser("effect", "src/main/resources/parseable/effect.xml"));   // for now no reverse search
        parsers.add(new Parser("harness", "src/main/resources/parseable/harness.xml"));   // for now no reverse search

    }

    public Index populateConfigIndex() {
        Index index = new Index();
        for (Parser parser : parsers) {
            index.update(parser.populateConfigIndex());
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
