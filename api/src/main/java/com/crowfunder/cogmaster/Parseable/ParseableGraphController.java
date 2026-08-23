package com.crowfunder.cogmaster.Parseable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/parseableGraph")
public class ParseableGraphController {
    private final ParseableGraphRepository repository;

    public ParseableGraphController(ParseableGraphRepository repository) {
        this.repository = repository;
    }

    public class GraphNode {
        public String path;
        public ArrayList<GraphNode> children = new ArrayList<>();
    }

    // endpoint used to draw a graph of all our entries on local UI
    @GetMapping("all")
    public ResponseEntity<Collection<GraphNode>> getAllconfigs() {
        var allNodesMap = new HashMap<String, GraphNode>();
        var nodeRootsMap = new HashMap<String, GraphNode>();

        var allRecords = repository.getAll();
        for (var configFileName : allRecords.keySet()) {
            var fileRecords = allRecords.get(configFileName);
            for (var path : fileRecords.keySet()) {
                var entry = fileRecords.get(path);
                var currentPath = path.getPath();
                var currentNode = allNodesMap.computeIfAbsent(currentPath, p -> {
                    var n = new GraphNode();
                    n.path = p;
                    return n;
                });

                var depthCounter = 0;
                while (entry.parentReference != null) {
                    if (depthCounter > 100)
                        return ResponseEntity.internalServerError().build(); // never happens

                    var parentPath = entry.parentReference.referencedEntry.path.getPath();
                    var parentNode = allNodesMap.computeIfAbsent(parentPath, p -> {
                        var n = new GraphNode();
                        n.path = p;
                        return n;
                    });

                    // this does happen
                    // if (parentNode.children.contains(currentNode)) 
                    //     return ResponseEntity.internalServerError().build();

                    parentNode.children.add(currentNode);

                    entry = entry.parentReference.referencedEntry;
                    currentNode = parentNode;
                    depthCounter++;
                }
               final var finalCurrentNode = currentNode;
                nodeRootsMap.computeIfAbsent(currentNode.path, n -> finalCurrentNode);
            }
        }

        return ResponseEntity.ok(nodeRootsMap.values());
    }
}
