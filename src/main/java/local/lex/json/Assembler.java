package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Собирает исходный json из частей на которые он был разделен
 */
public interface Assembler {

    /**
     * Собрать json из частей на которые он был разделен
     *
     * @param partitions части json'а
     * @return собранный json
     */
    JsonNode assemble(List<Partition> partitions);
}
