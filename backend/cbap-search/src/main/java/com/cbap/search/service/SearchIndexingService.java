package com.cbap.search.service;

import com.cbap.persistence.entity.EntityDefinition;
import com.cbap.persistence.entity.EntityRecord;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Service for indexing entity records in OpenSearch.
 */
@Service
public class SearchIndexingService {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexingService.class);

    private final OpenSearchClient openSearchClient;
    private final IndexService indexService;
    private final DenormalizationService denormalizationService;

    public SearchIndexingService(
            OpenSearchClient openSearchClient,
            IndexService indexService,
            DenormalizationService denormalizationService) {
        this.openSearchClient = openSearchClient;
        this.indexService = indexService;
        this.denormalizationService = denormalizationService;
    }

    /**
     * Index an entity record.
     */
    public void indexRecord(EntityDefinition entity, EntityRecord record) {
        try {
            // Ensure index exists
            if (!indexService.indexExists(entity.getEntityId())) {
                indexService.createIndex(entity.getEntityId());
            }

            // Extract denormalized fields
            Map<String, Object> recordData = record.getDataJson() != null 
                    ? record.getDataJson() 
                    : Map.of();
            
            Map<String, Object> indexedFields = denormalizationService.extractDenormalizedFields(entity, recordData);

            // Add record metadata
            indexedFields.put("recordId", record.getRecordId().toString());
            indexedFields.put("schemaVersion", record.getSchemaVersion());
            indexedFields.put("state", record.getState());
            indexedFields.put("createdAt", record.getCreatedAt() != null ? record.getCreatedAt().toString() : null);
            indexedFields.put("updatedAt", record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
            indexedFields.put("deleted", record.getDeletedAt() != null);

            // Index the document
            String indexName = indexService.getIndexName(entity.getEntityId());
            IndexRequest<Map<String, Object>> request = new IndexRequest.Builder<Map<String, Object>>()
                    .index(indexName)
                    .id(record.getRecordId().toString())
                    .document(indexedFields)
                    .build();

            openSearchClient.index(request);
            logger.debug("Indexed record: entityId={}, recordId={}", entity.getEntityId(), record.getRecordId());

        } catch (IOException e) {
            logger.error("Error indexing record: entityId={}, recordId={}", 
                    entity.getEntityId(), record.getRecordId(), e);
            // Don't throw - indexing failures shouldn't break record operations
        }
    }

    /**
     * Remove a record from the index (for soft deletes).
     */
    public void removeRecord(String entityId, UUID recordId) {
        try {
            String indexName = indexService.getIndexName(entityId);
            if (indexService.indexExists(entityId)) {
                DeleteRequest request = new DeleteRequest.Builder()
                        .index(indexName)
                        .id(recordId.toString())
                        .build();

                openSearchClient.delete(request);
                logger.debug("Removed record from index: entityId={}, recordId={}", entityId, recordId);
            }
        } catch (IOException e) {
            logger.error("Error removing record from index: entityId={}, recordId={}", entityId, recordId, e);
            // Don't throw - indexing failures shouldn't break record operations
        }
    }
}
