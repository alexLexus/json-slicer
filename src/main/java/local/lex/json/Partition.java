package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Часть json
 *
 * @param path путь до этого куска в исходном json
 * @param data потенциально разделенные данные
 */
public record Partition(PartitionPath path, JsonNode data) {

}
