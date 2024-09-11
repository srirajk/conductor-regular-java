package org.example.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import com.netflix.conductor.common.run.Workflow;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;


public class HumanTaskProcess {

    public static void main(String[] args) throws InterruptedException {

        ObjectMapper objectMapper = new ObjectMapper();

        OrkesClients orkesClients = ApiUtil.getOrkesClient();
        TaskClient taskClient = orkesClients.getTaskClient();
        WorkflowClient workflowClient = orkesClients.getWorkflowClient();
        MetadataClient metadataClient = orkesClients.getMetadataClient();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the workflow ID: ");
        String workflowId = scanner.nextLine();

        //System.out.println("Entered Workflow Id: " + workflowId);

        Workflow wf = workflowClient.getWorkflow(workflowId, false);

        Workflow.WorkflowStatus status = wf.getStatus();


        while (status != Workflow.WorkflowStatus.COMPLETED && status != Workflow.WorkflowStatus.FAILED) {
            wf = workflowClient.getWorkflow(workflowId, true);
            List<Task> tasks = wf.getTasks();
            loopOnHumanTasks(taskClient, objectMapper, workflowId, tasks);
            status = getWorkFlowStatus(workflowClient, workflowId);
            //System.out.println("Current Workflow Status: " + status.name());
            Thread.sleep(1000);
        }

        System.out.println("Workflow Final Status :: " + status.name() + " for workflow id: " + workflowId + " with output: " + wf.getOutput());


    }

    private static void loopOnHumanTasks(TaskClient taskClient, ObjectMapper objectMapper, String wfId, List<Task> tasks) {
        Scanner scanner = new Scanner(System.in);

        List<Task> humanTasks = tasks.stream().filter(task -> task.getStatus() == Task.Status.IN_PROGRESS && task.getReferenceTaskName().startsWith("human-task"))
                .collect(Collectors.toList());

        humanTasks.forEach(task -> {
            System.out.println("Enter the Decision for the Task: " + task.getReferenceTaskName() + " id: " + task.getTaskId());
            String decision = scanner.nextLine();
            TaskResult taskresult = new TaskResult();
            taskresult.setTaskId(task.getTaskId());
            taskresult.setWorkflowInstanceId(wfId);
            taskresult.setStatus(TaskResult.Status.COMPLETED);
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("decision", decision);
            taskresult.setOutputData((Map) objectMapper.convertValue(objectNode, Map.class));
            taskClient.updateTask(taskresult);
        });

    }

    private static Workflow.WorkflowStatus getWorkFlowStatus(WorkflowClient workflowClient, String workflowId) {
        Workflow wf = workflowClient.getWorkflow(workflowId, false);
        return wf.getStatus();
    }
}
