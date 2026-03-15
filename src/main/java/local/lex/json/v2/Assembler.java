package local.lex.json.v2;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Assembler {

    record PathKey(List<PathKeyEntry> entries) implements Comparable<PathKey> {

        public static PathKey of(Chunk chunk) {
            return new PathKey(chunk.path().getFragments().stream()
                    .map(it -> new PathKeyEntry(it.name(), it.index()))
                    .toList());
        }

        @Override
        public int compareTo(PathKey other) {
            List<PathKeyEntry> a = this.entries();
            List<PathKeyEntry> b = other.entries();
            int n = Math.min(a.size(), b.size());
            for (int i = 0; i < n; i++) {
                int cmp = a.get(i).compareTo(b.get(i));
                if (cmp != 0) return cmp;
            }
            return Integer.compare(a.size(), b.size());
        }
    }

    record PathKeyEntry(String name, Integer index) implements Comparable<PathKeyEntry> {

        @Override
        public int compareTo(PathKeyEntry o) {
            // Сначала сравниваем index (nullsLast)
            if (this.index == null && o.index != null) return 1;
            if (this.index != null && o.index == null) return -1;
            if (this.index != null && o.index != null) {
                int cmp = Integer.compare(this.index, o.index);
                if (cmp != 0) return cmp;
            }
            // Затем сравниваем name (nullsLast), лексикографически
            if (this.name == null && o.name != null) return 1;
            if (this.name != null && o.name == null) return -1;
            if (this.name == null && o.name == null) return 0;
            return this.name.compareTo(o.name);
        }
    }

    public JsonNode assemble(List<Chunk> chunks) {
        Map<PathKey, List<Chunk>> groupedAndSorted = groupAndSort(chunks);

        System.out.println("in " + chunks.size() + " out " + groupedAndSorted.size());
        groupedAndSorted.values().stream()
                .flatMap(Collection::stream)
                .forEach(System.out::println);

        return null;
    }


    private TreeMap<PathKey, List<Chunk>> groupAndSort(List<Chunk> chunks) {
        return chunks.stream().collect(Collectors.groupingBy(PathKey::of, TreeMap::new, collectAndSort()));
    }

    private Collector<Chunk, Object, List<Chunk>> collectAndSort() {
        return Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                .sorted(Comparator.comparingInt(chunk -> chunk.path().getFragments().getLast().start()))
                .toList()
        );
    }
}
