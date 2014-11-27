package com.liquigraph.core.writer;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.liquigraph.core.model.Changeset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.String.format;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardOpenOption.APPEND;

public class ChangelogFileWriter implements ChangelogWriter {

    private final PreconditionPrinter preconditionPrinter;
    private final File outputFile;

    public ChangelogFileWriter(PreconditionPrinter preconditionPrinter, File outputFile) {
        this.preconditionPrinter = preconditionPrinter;
        this.outputFile = outputFile;
    }

    @Override
    public void write(Collection<Changeset> changelogsToInsert) {
        try {
            reinitializeFile(outputFile.toPath());
            for (Changeset changeset : changelogsToInsert) {
                writeChangeset(changeset, outputFile.toPath());
            }
        } catch (IOException e) {
            throw propagate(e);
        }
    }

    private void reinitializeFile(Path path) throws IOException {
        deleteIfExists(path);
        createFile(path);
    }

    private void writeChangeset(Changeset changeset, Path path) throws IOException {
        Files.write(path, preconditionPrinter.print(changeset.getPrecondition()), Charsets.UTF_8, APPEND);
        Files.write(path, changesetToString(changeset), Charsets.UTF_8, APPEND);
    }

    private Collection<String> changesetToString(Changeset changeset) {
        Collection<String> lines = newLinkedList();
        lines.add(format("//Liquigraph changeset[author: %s, id: %s]", changeset.getAuthor(), changeset.getId()));
        lines.add(format("//Liquigraph changeset[executionContexts: %s]", flatten(changeset.getExecutionsContexts())));
        lines.add(changeset.getQuery());
        return lines;
    }

    private String flatten(Collection<String> executionsContexts) {
        if (executionsContexts.isEmpty()) {
            return "none declared";
        }
        return Joiner.on(",").join(executionsContexts);
    }

}