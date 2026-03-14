package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.unit.DataSize;

import java.util.List;

public interface Partitioner {

    /**
     * Разделить json на части
     *
     * @param json исходный json
     * @param limit максимальный размер в байтах для текстовых узлов json
     * @return список частей json с информацией для восстановления исходного json
     */
    List<Partition> split(JsonNode json, DataSize limit);
}
