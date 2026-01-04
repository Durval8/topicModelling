package com.topicmodelling.mallet;

import cc.mallet.types.Instance;

import java.util.Iterator;
import java.util.List;

public class BatchIterator implements Iterator<Instance> {

    private final Iterator<BatchEvent> iterator;

    public BatchIterator(List<BatchEvent> batchEvents) {
        this.iterator = batchEvents.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Instance next() {
        BatchEvent event = iterator.next();
        Doc doc = event.getDoc();
        System.out.println("GOT DOC FROM BATCH: " + doc.getTitle());
        String text = normalize(doc.getRawContent());
        String label = doc.getDate();
        String id  = doc.getArticleId();
        String name = doc.getTitle();

        return new Instance(text, label, name, id);
    }

    private static String normalize(String text) {
        // cheaper than regex replaceAll
        return text.replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ');
    }
}
