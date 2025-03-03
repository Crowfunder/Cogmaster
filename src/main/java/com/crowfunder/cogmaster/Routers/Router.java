package com.crowfunder.cogmaster.Routers;

import com.crowfunder.cogmaster.Configs.Path;
import java.util.HashMap;

public record Router(String implementation, HashMap<String, Path> routes) {

}
