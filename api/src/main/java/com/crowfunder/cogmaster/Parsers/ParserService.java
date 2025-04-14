package com.crowfunder.cogmaster.Parsers;

import com.crowfunder.cogmaster.CogmasterConfig;
import com.crowfunder.cogmaster.Index.Index;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParserService {

    private final CogmasterConfig cogmasterConfig;

    private final List<Parser> parsers;
    private final List<String> parserNames;
    private final String parseablePath;

    public ParserService(CogmasterConfig cogmasterConfig) {

        // Initialize existing parsers, probably not the best idea to do it like that but oh well
        this.cogmasterConfig = cogmasterConfig;
        this.parsers = new ArrayList<>();
        this.parserNames = cogmasterConfig.getParsers().getList();
        this.parseablePath = cogmasterConfig.getParsers().getPath();

        for (String parserName : this.parserNames) {
            this.parsers.add(new Parser(parserName, "/" + parseablePath + "/" + parserName + ".xml"));
        }

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
