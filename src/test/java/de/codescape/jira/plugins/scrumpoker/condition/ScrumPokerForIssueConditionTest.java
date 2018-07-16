package de.codescape.jira.plugins.scrumpoker.condition;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import de.codescape.jira.plugins.scrumpoker.service.EstimationFieldService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ScrumPokerForIssueConditionTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private CustomFieldManager customFieldManager;

    @Mock
    private EstimationFieldService estimationFieldService;

    @Mock
    private ApplicationUser applicationUser;

    @Mock
    private JiraHelper jiraHelper;

    @InjectMocks
    private ScrumPokerForIssueCondition scrumPokerForIssueCondition;

    @Mock
    private CustomField storyPointField;

    @Mock
    private CustomField someOtherField;

    @Mock
    private Issue issue;

    @Before
    public void before() {
        // configured field for Scrum Poker
        when(estimationFieldService.findStoryPointField()).thenReturn(storyPointField);

        // expected ids for fields
        when(storyPointField.getId()).thenReturn("STORY_POINT_FIELD_ID");
        when(someOtherField.getId()).thenReturn("SOME_OTHER_FIELD_ID");

        when(jiraHelper.getContextParams()).thenReturn(mapThatContainsTheIssue());
    }

    @Test
    public void shouldDisplayForEditableIssueWithStoryPointField() {
        expectThatIssueContainsTheStoryPointField();
        expectThatIssueIsEditable();
        assertThat(scrumPokerForIssueCondition.shouldDisplay(applicationUser, jiraHelper), is(true));
    }

    @Test
    public void shouldNotDisplayForEditableIssueWithoutStoryPointField() {
        expectThatIssueDoesNotContainTheStoryPointField();
        expectThatIssueIsEditable();
        assertThat(scrumPokerForIssueCondition.shouldDisplay(applicationUser, jiraHelper), is(false));
    }

    @Test
    public void shouldNotDisplayForNonEditableIssue() {
        expectThatIssueIsNotEditable();
        assertThat(scrumPokerForIssueCondition.shouldDisplay(applicationUser, jiraHelper), is(false));
    }

    private void expectThatIssueIsNotEditable() {
        when(issue.isEditable()).thenReturn(false);
    }

    private void expectThatIssueIsEditable() {
        when(issue.isEditable()).thenReturn(true);
    }

    private void expectThatIssueDoesNotContainTheStoryPointField() {
        List<CustomField> emptyList = new ArrayList<>();
        emptyList.add(someOtherField);
        when(customFieldManager.getCustomFieldObjects(issue)).thenReturn(emptyList);
    }

    private void expectThatIssueContainsTheStoryPointField() {
        ArrayList<CustomField> listOfCustomFields = new ArrayList<>();
        listOfCustomFields.add(storyPointField);
        listOfCustomFields.add(someOtherField);
        when(customFieldManager.getCustomFieldObjects(issue)).thenReturn(listOfCustomFields);
    }

    private HashMap<String, Object> mapThatContainsTheIssue() {
        HashMap<String, Object> contextParams = new HashMap<>();
        contextParams.put("issue", issue);
        return contextParams;
    }

}
