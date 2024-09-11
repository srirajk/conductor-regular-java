package org.example.local;


import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;
import io.orkes.conductor.client.automator.TaskRunnerConfigurer;
import org.example.local.workflow.ApprovalWorkflow;
import org.example.local.workflow.CaseRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TriggerWorkers {

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        OrkesClients orkesClients = ApiUtil.getOrkesClient();
        TaskClient taskClient = orkesClients.getTaskClient();
        WorkflowClient workflowClient = orkesClients.getWorkflowClient();
        MetadataClient metadataClient = orkesClients.getMetadataClient();

        WorkflowExecutor workflowExecutor = new WorkflowExecutor(taskClient, workflowClient, metadataClient, 10);
        workflowExecutor.initWorkers("org.example.local.workers");
        TaskRunnerConfigurer taskrunner = initWorkers(Arrays.asList(),taskClient);



       /* CreateWorkflow workflowCreator = new CreateWorkflow(workflowExecutor);
        ConductorWorkflow<WorkflowInput> simpleWorkflow = workflowCreator.createGreetingsWorkflow();
        simpleWorkflow.setVariables(new HashMap<>());

        WorkflowInput input = new WorkflowInput("This is Sriraj Java first workflow run");
        CompletableFuture<Workflow> workflowExecution = simpleWorkflow.executeDynamic(input);
        Workflow workflowRun = workflowExecution.get(60, TimeUnit.SECONDS);
        workflowRun.getOutput().forEach((k, v) -> System.out.println(k + " : " + v));*/

/*        ApprovalWorkflow approvalWorkflow = new ApprovalWorkflow(workflowExecutor);
        ConductorWorkflow<CaseRequest> approvalWorkflow1 = approvalWorkflow.createApprovalWorkflow();

        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseId(String.valueOf(generateRandomFourDigitNumber()));
        caseRequest.setCaseType("DEMO");
        caseRequest.setCaseStatus("INITIALIZED");
        caseRequest.setCaseDescription("This is a demo case");

        CompletableFuture<Workflow> wfExecute = approvalWorkflow1.execute(caseRequest);
        Workflow workflowResponse = wfExecute.get(1, TimeUnit.MINUTES);

        System.out.println("Workflow Id: " + workflowResponse);
        System.out.println("Workflow Status: " + workflowResponse.getStatus());
        System.out.println("Workflow Output: " + workflowResponse.getOutput());

        taskrunner.shutdown();
        workflowClient.shutdown();*/
        //System.exit(0);
    }

    public static int generateRandomFourDigitNumber() {
        Random random = new Random();
        return 1000 + random.nextInt(9000);
    }

    private static TaskRunnerConfigurer initWorkers(List<Worker> workers, TaskClient taskClient) {
        TaskRunnerConfigurer.Builder builder = new TaskRunnerConfigurer.Builder(taskClient, workers);
        TaskRunnerConfigurer taskRunner = builder.withThreadCount(1).withTaskPollTimeout(5).build();
        // Start Polling for tasks and execute them
        taskRunner.init();
        return taskRunner;
    }
}
