package local.lex.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.lang.Nullable;

import java.util.List;

public interface PartitionPath extends Comparable<PartitionPath> {

    /**
     * Имя корневого узла, по контракту всегда равен $
     */
    String ROOT_PATH_NAME = "$";

    /**
     * Индекс корневого узла, по контракту всегда равен -1
     */
    int ROOT_PATH_INDEX = -1;

    /**
     * Индексы начала и конца части, которая не разделялась
     */
    int NOT_PARTITIONED_START_END = -1;

    /**
     * Тип элемента пути, служит для корректного восстановления json после разделения на куски
     */
    enum ElementKind {
        /**
         * Представляет json массив
         */
        ARRAY,

        /**
         * Представляет json объект
         */
        OBJECT,

        /**
         * Представляет любой лист json дерева
         */
        VALUE;

        /**
         * Вернет тип элемента пути, который соответствует типу узла json дерева
         * @param json узел для которого нужно определить тип элемента пути
         * @return соответсвующий элемент пути
         */
        public static ElementKind getMatchedType(JsonNode json) {
            return switch (json.getNodeType()) {
                case ARRAY -> ARRAY;
                case OBJECT -> OBJECT;
                default -> VALUE;
            };
        }
    }

    /**
     * Элемент пути до json поля.
     * По контракту, если элемент это элемент массива, то name должен быть равен null.
     *
     * @param kind  тип элемента
     * @param name  имя элемента, для элементов массива всегда null
     * @param index индекс элемента в массиве или порядок следования поля в объекте
     * @param start позиция начала среза в исходных данных
     * @param end позиция конца среза в исходных данных
     */
    record Element(ElementKind kind, @Nullable String name, int index, int start, int end) {

        /**
         * Так как по контракту для элементов массива name всегда должен быть равен null, можно использовать как
         * маркер того что текущий элемент это элемент массива
         *
         * @return название поля в родительском объекте или null, если родительский элемент это массив
         */
        boolean hasName() {
            return name != null;
        }

        /**
         * Указывает ли текущий элемент пути на корень json или нет.
         * По контракту корневой элемент пути всегда имеет имя "$" и индекс -1
         *
         * @return true если текущий путь указывает на корень json.
         */
        boolean isRoot() {
            return ROOT_PATH_NAME.equals(name) && ROOT_PATH_INDEX == index;
        }

        /**
         * Соответствует ли текущий элемент пути json элементу
         *
         * @param json json удел для проверки
         * @return true, если элемент соответствует, иначе false
         */
        public boolean isMatchesWith(JsonNode json) {
            return this.kind() == ElementKind.getMatchedType(json);
        }


    }

    /**
     * @return копия списка элементов пути
     */
    List<Element> elements();

    /**
     * @param element элемент, который будет добавлен в конец пути
     */
    void append(Element element);

    /**
     * @param element элемент, который будет добавлен в начало пути
     */
    void prepend(Element element);

    /**
     * @return первый элемент пути, который будет удален из списка элементов
     */
    Element removeFirst();

    /**
     * @return последний элемент пути, который будет удален из списка элементов
     */
    Element removeLast();

    /**
     * @return последний элемент пути
     */
    Element getLast();

    /**
     * @return true, если в пути больше нет элементов
     */
    boolean isEmpty();

    /**
     * @return копия этого пути
     */
    PartitionPath copy();
}
