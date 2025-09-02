package org.aleksid.my_ai.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aleksid.my_ai.model.LoadedDocument;
import org.aleksid.my_ai.repository.DocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentLoadingService implements CommandLineRunner {
    private final DocumentRepository documentRepository;
    private final ResourcePatternResolver resolver;
    private final VectorStore vectorStore;

    @SneakyThrows
    public void loadDocuments() {
        List<Resource> resources = Arrays.stream(
                resolver.getResources("classpath:/knowledgebase/**/*.txt")
        ).toList();
        resources.stream()
                .map(resource -> Pair.of(resource, calcContentHash(resource)))
                .filter(pair -> !documentRepository.existsByFilenameAndContentHash(pair.getFirst().getFilename(), pair.getSecond()))
                .forEach(pair -> {
                    Resource resource = pair.getFirst();
                    List<Document> documents = new TextReader(resource).get();
                    TokenTextSplitter textSplitter = TokenTextSplitter.builder().withChunkSize(500).build();
                    List<Document> chunks = textSplitter.apply(documents);
                    vectorStore.accept(chunks);

                    LoadedDocument loadedDocument = LoadedDocument.builder()
                            .filename(resource.getFilename())
                            .contentHash(pair.getSecond())
                            .documentType("txt")
                            .chunkCount(chunks.size())
                            .build();

                    documentRepository.save(loadedDocument);
                });
    }

    @SneakyThrows
    private String calcContentHash(Resource resource) {
        return DigestUtils.md5DigestAsHex(resource.getContentAsByteArray());
    }


    @Override
    public void run(String... args) throws Exception {
        loadDocuments();
    }
}
