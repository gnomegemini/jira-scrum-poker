package net.congstar.jira.plugins.scrumpoker.action;

import net.congstar.jira.plugins.scrumpoker.data.PlanningPokerStorage;

/**
 * Persist a chosen card of a user for a given issue into the plugin storage and redirect to the planning poker page.
 */
public class ChooseCardAction extends ScrumPokerAction {

    private static final long serialVersionUID = 1L;

    private final PlanningPokerStorage planningPokerStorage;

    public ChooseCardAction(PlanningPokerStorage planningPokerStorage) {
        this.planningPokerStorage = planningPokerStorage;
    }

    @Override
    protected String doExecute() throws Exception {
        String chosenCard = getHttpRequest().getParameter("chosenCard");
        String issueKey = getHttpRequest().getParameter(PARAM_ISSUE_KEY);

        planningPokerStorage.sessionForIssue(issueKey).updateCard(getLoggedInUser().getKey(), chosenCard);

        return getRedirect("/secure/scrumPokerStart.jspa?issueKey=" + issueKey);
    }

}
