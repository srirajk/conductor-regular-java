package org.example.local;

import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;
import io.orkes.conductor.client.MetadataClient;
import io.orkes.conductor.client.OrkesClients;
import io.orkes.conductor.client.TaskClient;
import io.orkes.conductor.client.WorkflowClient;
import org.example.local.workflow.ApprovalWorkflowWaitTasks;
import org.example.local.workflow.CaseRequest;

public class RegisterWorkflow {

    public static void main(String[] args) {

        OrkesClients orkesClients = ApiUtil.getOrkesClient();
        TaskClient taskClient = orkesClients.getTaskClient();
        WorkflowClient workflowClient = orkesClients.getWorkflowClient();
        MetadataClient metadataClient = orkesClients.getMetadataClient();

       // WorkflowExecutor workflowExecutor = new WorkflowExecutor(taskClient, workflowClient, metadataClient, 10);

        WorkflowExecutor workflowExecutor = new WorkflowExecutor(taskClient, workflowClient, metadataClient, 10);

        ApprovalWorkflowWaitTasks approvalWorkflowWaitTasks = new ApprovalWorkflowWaitTasks(workflowExecutor);

        ConductorWorkflow<CaseRequest> approvalWorkflow = approvalWorkflowWaitTasks.createApprovalWorkflow();

        System.out.println("Workflow created: " + approvalWorkflow.getName() + " version: " + approvalWorkflow.getVersion());

        System.exit(0);



    }

}
