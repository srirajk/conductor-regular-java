package org.example.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;
import io.orkes.conductor.client.automator.TaskRunnerConfigurer;
import org.example.local.workflow.ApprovalWorkflowWaitTasks;
import org.example.local.workflow.CaseRequest;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class TriggerWorkflow {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        ObjectMapper objectMapper = new ObjectMapper();

        OrkesClients orkesClients = ApiUtil.getOrkesClient();
        TaskClient taskClient = orkesClients.getTaskClient();
        WorkflowClient workflowClient = orkesClients.getWorkflowClient();
        MetadataClient metadataClient = orkesClients.getMetadataClient();

        WorkflowExecutor workflowExecutor = new WorkflowExecutor(taskClient, workflowClient, metadataClient, 10);
   /*     workflowExecutor.initWorkers("org.example.local.workers");
        TaskRunnerConfigurer taskrunner = initWorkers(Arrays.asList(), taskClient);*/

/*        ApprovalWorkflowWaitTasks approvalWorkflowWaitTasks = new ApprovalWorkflowWaitTasks(workflowExecutor);

        ConductorWorkflow<CaseRequest> workflow = approvalWorkflowWaitTasks.createApprovalWorkflow();*/

        CaseRequest caseRequest = new CaseRequest();
        String caseId = UUID.randomUUID().toString();
        caseRequest.setCaseId(caseId);
        caseRequest.setCaseType("DEMO");
        caseRequest.setCaseStatus("INITIALIZED");
        caseRequest.setCaseDescription("This is a demo case");

        Map<String, Object> inputMap = (Map) objectMapper.convertValue(caseRequest, Map.class);

        String workflowName = "CaseApprovalWorkflow";
        int version = 1;

        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName(workflowName);
        startWorkflowRequest.setVersion(version);
        startWorkflowRequest.setCorrelationId("corr-"+caseId);
        startWorkflowRequest.setInput(inputMap);

        String workflowId = workflowClient.startWorkflow(startWorkflowRequest);
        System.out.println("Workflow Id: " + workflowId);

        System.exit(0);

       // String workflowId = "3051cdf1-6c6c-11ef-af9e-0242ac110002";

/*        CompletableFuture<Workflow> wfExecute = workflow.execute(caseRequest);
        Workflow workflowResponse = wfExecute.get(1, TimeUnit.MINUTES);
        String wfId = workflowResponse.getWorkflowId();*/


/*
        System.out.println("Workflow Id: " + workflowId);
        Workflow wf = workflowClient.getWorkflow(workflowId, true);
        List<Task> tasks = wf.getTasks();

        tasks.forEach(task -> {
            System.out.println("Task: " + task.getTaskType() + " Status: " + task.getStatus());
        });

        List<Task> humanTasks = tasks.stream().filter(task -> task.getReferenceTaskName().startsWith("human-task"))
                .collect(Collectors.toList());


        humanTasks.forEach(task -> {
            System.out.println("Task: " + task.getReferenceTaskName() + " Status: " + task.getStatus());
        });
*/







       /* Map<String, Map<String, String>> humanTaskDetails = tasks.stream()
                .filter(task -> task.getReferenceTaskName().startsWith("human-task"))
                .map(task -> {
                    String[] parts = task.getReferenceTaskName().split("_");
                    Map<String, String> innerMap = new HashMap<>();
                    innerMap.put(parts[1], task.getTaskId());
                    return new AbstractMap.SimpleEntry<>(parts[0], innerMap);
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> {
                            existing.putAll(replacement);
                            return existing;
                        }
                ));

        humanTaskDetails.forEach((key, value) -> {
            System.out.println("Queue Name: " + key);
            value.forEach((k, v) -> {
                System.out.println(" Task Name: "+ k + " Task Id: " + v);
            });
        });*/


    }

    private static TaskRunnerConfigurer initWorkers(List<Worker> workers, TaskClient taskClient) {
        TaskRunnerConfigurer.Builder builder = new TaskRunnerConfigurer.Builder(taskClient, workers);
        TaskRunnerConfigurer taskRunner = builder.withThreadCount(1).withTaskPollTimeout(5).build();
        // Start Polling for tasks and execute them
        taskRunner.init();
        return taskRunner;
    }

}
