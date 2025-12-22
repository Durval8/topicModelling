package com.topicmodelling;


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


public class LambdaHandler implements RequestHandler<Map<String,String>, Void>{

    @Override
    public Void handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        logger.log("EVENT TYPE: " + event.getClass());
        return null;
    }
}

//
//public class LambdaHandler implements RequestHandlerr<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
////    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
////    static {
////        try {
////            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(Application.class);
////        } catch (ContainerInitializationException e) {
////            // if we fail here. We re-throw the exception to force another cold start
////            e.printStackTrace();
////            throw new RuntimeException("Could not initialize Spring Boot application", e);
////        }
////    }
//
//    @Override
//    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context){
//
//    }
//
////    @Override
////    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
////            throws IOException {
////        handler.proxyStream(inputStream, outputStream, context);
////    }
//}