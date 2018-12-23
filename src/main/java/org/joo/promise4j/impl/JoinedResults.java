package org.joo.promise4j.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JoinedResults<D> implements Iterable<D> {

    private final List<D> results;

    public JoinedResults(List<D> results) {
        this.results = Collections.unmodifiableList(results);
    }

    public D get(int idx) {
        return results.get(idx);
    }

    @Override
    public Iterator<D> iterator() {
        return results.iterator();
    }

    public int size() {
        return results.size();
    }
}
