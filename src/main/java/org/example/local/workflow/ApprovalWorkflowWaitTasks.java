package org.example.local.workflow;

import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.sdk.workflow.def.ConductorWorkflow;
import com.netflix.conductor.sdk.workflow.def.WorkflowBuilder;
import com.netflix.conductor.sdk.workflow.def.tasks.*;
import com.netflix.conductor.sdk.workflow.executor.WorkflowExecutor;

import java.time.Duration;

public class ApprovalWorkflowWaitTasks {

    private final WorkflowExecutor executor;


    public ApprovalWorkflowWaitTasks(WorkflowExecutor executor) {
        this.executor = executor;
    }


    public ConductorWorkflow<CaseRequest> createApprovalWorkflow() {

        WorkflowBuilder<CaseRequest> workflowBuilder = new WorkflowBuilder<>(executor);

       /* SimpleTask level1Investigator1 = new SimpleTask("add_operator_review",
                "level1_investigator_review_1")
                .input("caseId", "${workflow.input.caseId}")
                .input("currentStage", "level1_investigator_review_1");*/
        Wait level1Investigator1 = new Wait("human-task_level1_investigator-review-1", Duration.ofMillis(2000000));


    /*    SimpleTask level1Investigator2 = new SimpleTask("add_operator_review",
                "level1_investigator_review_2")
                .input("caseId", "${workflow.input.caseId}")
                .input("currentStage", "level1_investigator_review_2");*/



        /*Wait waitTask2 = new Wait("wait_for_2_sec", Duration.ofMillis(2000));*/
        Wait level1Investigator2 = new Wait("human-task_level1_investigator-review-2", Duration.ofMillis(2000000));

  /*      SimpleTask level2Investigator = new SimpleTask("add_operator_review",
                "level2_investigator_review")
                .input("caseId", "${workflow.input.caseId}")
                .input("currentStage", "level2_investigator_review_1");*/

        Wait level2Investigator = new Wait("human-task_level2_investigator-review", Duration.ofMillis(2000000));

        String concatenateLevel1Decisions = "function concatenateDecisions(level1Decision1, level1Decision2) {" +
                "    return level1Decision1 + '_' + level1Decision2;" +
                "}" +
                "concatenateDecisions('${human-task_level1_investigator-review-1.output.decision}', '${human-task_level1_investigator-review-2.output.decision}');";

        ConductorWorkflow<CaseRequest> workflow = workflowBuilder.name("CaseApprovalWorkflow")
                .version(1)
                .ownerEmail("caseTeam@org.com")
                .variables(new CaseWorkflowState())
                .description("Case approval workflow")
                .add(new SetVariable("set_case_metadata_state_in_wf")
                        .input("caseId", "${workflow.input.caseId}")
                        .input("caseState", "IN_PROGRESS")
                        .input("caseType", "${workflow.input.caseType}")
                        .input("currentLevel", "Level1")
                )
                .add(new ForkJoin("level1_review",
                        new Task[]{level1Investigator1},
                        new Task[]{level1Investigator2})
                ).add(
                        new Javascript("concatenate_level1_decisions", concatenateLevel1Decisions)
                )
                .add(
                        new Switch("evaluate_level1_decisions", "${concatenate_level1_decisions.output.result}") //"${level1Decision1.output}_${level1Decision2.output}"
                                .switchCase(
                                        "NegativeMatch_NegativeMatch",
                                        new SetVariable("closing the case"),
                                        new Terminate(
                                                "level1_close_case",
                                                Workflow.WorkflowStatus.COMPLETED,
                                                "Both level 1 investigators Maker/Checker marked as a Negative match. Case closed.", "${concatenate_level1_decisions.output}"))
                                .switchCase(
                                        "TrueMatch_NegativeMatch",
                                        new SetVariable("TrueMatch_NegativeMatch_Set")
                                                .input("caseState", "LEVEL_2_IN_PROGRESS")
                                                .input("currentLevel", "Level2"))
                                .switchCase(
                                        "NegativeMatch_TrueMatch",
                                        new SetVariable("NegativeMatch_TrueMatch_Set")
                                                .input("caseState", "LEVEL_2_IN_PROGRESS")
                                                .input("currentLevel", "Level2")
                                )
                                .switchCase(
                                        "TrueMatch_TrueMatch",
                                        new SetVariable("TrueMatch_TrueMatch_Set")
                                                .input("caseState", "LEVEL_2_IN_PROGRESS")
                                                .input("currentLevel", "Level2")
                                )
                                .defaultCase(
                                        new Terminate(
                                                "level1_unexpected_decision",
                                                Workflow.WorkflowStatus.FAILED,
                                                "Level 1 Unexpected decision combination"))

                )

                .add(level2Investigator)
                .add(new Switch("level_2_decision", "${human-task_level2_investigator-review.output.decision}")
                        .switchCase(
                                "NegativeMatch",
                                new SetVariable("level2_negative_match_state_update")
                                        .input("caseState", "LEVEL_2_FINISHED")
                                        .input("currentLevel", "Level2"),
                                new Terminate(
                                        "level2_negative_match_close_case",
                                        Workflow.WorkflowStatus.COMPLETED,
                                        "Level 2 investigator marked as a Negative match. Case closed.", "${human-task_level2_investigator-review.output.decision}"))
                        .switchCase(
                                "TrueMatch",
                                new SetVariable("level2_true_match_state_update")
                                        .input("caseState", "LEVEL_2_FINISHED")
                                        .input("currentLevel", "Level2"),
                                new Terminate(
                                        "level2_true_match_close_case",
                                        Workflow.WorkflowStatus.COMPLETED,
                                        "Level 2 investigator marked as a True match. Case closed.", "${human-task_level2_investigator-review.output.decision}"))
                        .defaultCase(
                                new Terminate(
                                        "level2_unexpected_decision",
                                        Workflow.WorkflowStatus.FAILED,
                                        "Level 2 Unexpected decision combination"))
                )
                .build();

        workflow.registerWorkflow(true, true);

        return workflow;


    }


}
