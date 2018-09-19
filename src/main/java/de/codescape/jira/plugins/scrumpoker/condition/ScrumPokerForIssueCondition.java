package de.codescape.jira.plugins.scrumpoker.condition;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractIssueWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.web.Condition;
import de.codescape.jira.plugins.scrumpoker.service.EstimationFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This condition is used to decide whether a button to start a Scrum poker session should be displayed or not for the
 * given issue. The issue must be editable and have the custom field that is configured for Scrum poker estimations.
 */
@Component
public class ScrumPokerForIssueCondition extends AbstractIssueWebCondition implements Condition {

    private final CustomFieldManager customFieldManager;
    private final EstimationFieldService estimationFieldService;

    @Autowired
    public ScrumPokerForIssueCondition(CustomFieldManager customFieldManager,
                                       EstimationFieldService estimationFieldService) {
        this.customFieldManager = customFieldManager;
        this.estimationFieldService = estimationFieldService;
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue, JiraHelper jiraHelper) {
        return isEstimable(issue);
    }

    /**
     * Returns whether a Scrum poker session can be started for the given issue.
     */
    public boolean isEstimable(Issue issue) {
        return issue.isEditable() && hasStoryPointField(issue);
    }

    private boolean hasStoryPointField(Issue currentIssue) {
        CustomField storyPointField = estimationFieldService.findStoryPointField();
        return customFieldManager.getCustomFieldObjects(currentIssue).contains(storyPointField);
    }

}
