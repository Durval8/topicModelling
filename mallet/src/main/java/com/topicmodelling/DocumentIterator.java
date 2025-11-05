/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.topicmodelling;

import cc.mallet.types.Instance;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DocumentIterator implements Iterator<Instance> {
    private Scanner scanner;

    public DocumentIterator(InputStream dataInputStream) {
        scanner = new Scanner(dataInputStream);
    }

    public Instance next() {

        String line = scanner.nextLine();

        String[] lineEntries = line.split("\t");

        // line in the following form:
        // 14	2017-02	Homeland Security Secretary...
        String docId = lineEntries[0];  // identifier of the document
        String label = lineEntries[1];  // label of the document (in this case a date)
        String text = lineEntries[2];   // content (text) of the document
        return new Instance(text, label, docId, "");
    }

    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public void remove() {
        throw new IllegalStateException("This Iterator<Instance> does not support remove().");
    }
}

