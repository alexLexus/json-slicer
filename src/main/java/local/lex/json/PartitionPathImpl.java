package local.lex.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class PartitionPathImpl implements PartitionPath {

    // Версия контракта пути, на тот случай если измениться логика партиционирования или хэширования
    private final int version = 0;

    private final LinkedList<Element> elements;


    public PartitionPathImpl() {
        this.elements = new LinkedList<>();
    }

    private PartitionPathImpl(Collection<Element> elements) {
        this.elements = new LinkedList<>(elements);
    }

    public List<Element> elements() {
        return List.copyOf(elements);
    }

    public void append(Element element) {
        elements.addLast(element);
    }

    public void prepend(Element element) {
        elements.addLast(element);
    }

    public Element removeFirst() {
        return elements.removeFirst();
    }

    @Override
    public Element removeLast() {
        return elements.removeLast();
    }

    @Override
    public Element getLast() {
        return elements.getLast();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public PartitionPathImpl copy() {
        return new PartitionPathImpl(this.elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PartitionPathImpl other)) {
            return false;
        }

        return this.elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public int compareTo(PartitionPath other) {
        Iterator<Element> it1 = this.elements.iterator();
        Iterator<Element> it2 = other.elements().iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Element e1 = it1.next();
            Element e2 = it2.next();

            int cmp = compareElements(e1, e2);
            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(this.elements.size(), other.elements().size());
    }

    private int compareElements(Element a, Element b) {
        int cmp = Integer.compare(a.kind().ordinal(), b.kind().ordinal());
        if (cmp != 0) {
            return cmp;
        }

        String n1 = a.name();
        String n2 = b.name();
        if (n1 == null && n2 != null) {
            return -1;
        }
        if (n1 != null && n2 == null) {
            return 1;
        }

        if (n1 != null) {
            cmp = n1.compareTo(n2);
            if (cmp != 0) {
                return cmp;
            }
        }

        return Integer.compare(a.index(), b.index());
    }
}
