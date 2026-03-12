package local.lex.slice.v2.json;

@FunctionalInterface
public interface SizeMeter {

    long sizeOf(Object o) throws Exception;
}
