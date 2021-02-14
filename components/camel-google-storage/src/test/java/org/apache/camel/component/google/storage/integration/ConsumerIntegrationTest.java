package org.apache.camel.component.google.storage.integration;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.google.storage.GoogleCloudStorageConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

public class ConsumerIntegrationTest extends CamelTestSupport {

    @EndpointInject
    private ProducerTemplate template;

    @EndpointInject("mock:result")
    private MockEndpoint result;

    @EndpointInject("mock:consumedObjects")
    private MockEndpoint consumedObjects;

    private final String bucketName = "rafa_test_bucket";
    final String serviceAccountKey = "C:\\Users\\rmarc\\Desktop\\LABS\\GCP_Storage\\FunctionExampleProject-c59c4a999d8a.json";

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                String endpoint = String.format("google-storage://%s?serviceAccountKey=%s&autoCreateBucket=true", bucketName,
                        serviceAccountKey);
                //String endpoint = String.format("google-storage://%s?autoCreateBucket=true", bucketName);

                from("direct:putObject")
                        .startupOrder(1)
                        .to(endpoint)
                        .to("mock:result");

                from("google-storage://myCamelBucket?"
                     + "moveAfterRead=true"
                     + "&destinationBucket=camelDestinationBucket"
                     + "&autoCreateBucket=true"
                     + "&deleteAfterRead=true"
                     + "&includeBody=true")
                             .startupOrder(2)
                             .log("consuming: ${header.CamelGoogleCloudStorageBucketName}/${header.CamelGoogleCloudStorageObjectName}, body=${body}")
                             .to("mock:consumedObjects");

            }
        };
    }

    @Test
    public void sendIn() throws Exception {
        result.expectedMessageCount(3);
        consumedObjects.expectedMessageCount(3);

        //upload a files

        template.send("direct:putObject", exchange -> {
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OBJECT_NAME, "test.txt");
            exchange.getIn().setBody("Test");
        });

        template.send("direct:putObject", exchange -> {
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OBJECT_NAME, "test1.txt");
            exchange.getIn().setBody("Test1");
        });

        template.send("direct:putObject", exchange -> {
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OBJECT_NAME, "test2.txt");
            exchange.getIn().setBody("Test2");
        });
        /*
        Exchange listBucketsExchange = template.request("direct:listBucket", exchange -> {
            // exchange.getIn().setHeader(GoogleCloudStorageConstants.BUCKET_NAME, "myBucket"); not needed
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OPERATION, GoogleCloudStorageComponentOperations.listBuckets);
        });
        List<Bucket> bucketsList = listBucketsExchange.getMessage().getBody(List.class);
        LOG.info("bucketsList {}", bucketsList );
        
        
        Exchange listObjectsExchange = template.request("direct:listObjects", exchange -> {
            exchange.getIn().setHeader(GoogleCloudStorageConstants.OPERATION, GoogleCloudStorageComponentOperations.listObjects);
        });
        LOG.info("listObjectsExchange.body={}", listObjectsExchange.getMessage().getBody());
        */
        Thread.sleep(10000);
        assertMockEndpointsSatisfied();
    }

}