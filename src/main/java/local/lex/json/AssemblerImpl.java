package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.stream.Collectors;

public class AssemblerImpl implements Assembler {

    private final JsonNodeFactory factory;

    public AssemblerImpl() {
        this.factory = JsonNodeFactory.instance;
    }

    public AssemblerImpl(JsonNodeFactory factory) {
        this.factory = factory;
    }

    @Override
    public JsonNode assemble(List<Partition> partitions) {
        Map<PartitionPath, List<Partition>> groupedAndSorted = groupAndSort(partitions);
        JsonNode root = null;
        for (Map.Entry<PartitionPath, List<Partition>> entry : groupedAndSorted.entrySet()) {
            PartitionPath path = entry.getKey();
            List<Partition> sorted = entry.getValue();

            PartitionPath.Element first = path.removeFirst();
            if (root == null) {
                root = createJsonNode(first, sorted);
            }

            JsonNode parent = null;
            for (PartitionPath.Element element : path.elements()) {
                if (parent == null) {
                    parent = root;
                }

                JsonNode child = createJsonNode(element, sorted);

                if (parent instanceof ArrayNode array) {
                    int index = element.index();
                    if (array.size() > index) {
                        parent = array.get(index);
                    } else if (array.size() == index) {
                        array.insert(index, child);
                        parent = child;
                    } else {
                        throw new IllegalStateException("parent array");
                    }
                } else if (parent instanceof ObjectNode object) {
                    String name = element.name();
                    if (object.has(name)) {
                        parent = object.get(name);
                    } else {
                        object.set(name, child);
                        parent = child;
                    }
                }
            }
        }

        return root;
    }

    private JsonNode createJsonNode(PartitionPath.Element first, List<Partition> partitions) {
        return switch (first.kind()) {
            case ARRAY -> factory.arrayNode();
            case OBJECT -> factory.objectNode();
            case VALUE -> createValueJsonNode(partitions);
        };
    }

    private JsonNode createValueJsonNode(List<Partition> partitions) {
        return partitions.size() == 1
                ? partitions.get(0).data().deepCopy()
                : factory.textNode(partitions.stream()
                .map(it -> it.data().asText())
                .collect(Collectors.joining()));
    }

    private Map<PartitionPath, List<Partition>> groupAndSort(List<Partition> partitions) {
        Map<PartitionPath, List<Partition>> grouped = partitions.stream()
                .collect(Collectors.groupingBy(Partition::path));

        TreeMap<PartitionPath, List<Partition>> groupedAndSorted = new TreeMap<>();

        for (Map.Entry<PartitionPath, List<Partition>> entry : grouped.entrySet()) {
            List<Partition> sorted = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(it -> it.path().getLast().start()))
                    .toList();

            groupedAndSorted.put(entry.getKey(), sorted);
        }

        return groupedAndSorted;
    }
}
