package net.congstar.jira.plugins.scrumpoker.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.net.URL;

import net.congstar.jira.plugins.scrumpoker.data.PlanningPokerStorage;
import net.congstar.jira.plugins.scrumpoker.data.StoryPointFieldSupport;
import net.congstar.jira.plugins.scrumpoker.model.PokerCard;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

public final class StartPlanningPoker extends ScrumPokerAction {

    private static final long serialVersionUID = 1L;

    private final IssueManager issueManager;

    private final PlanningPokerStorage planningPokerStorage;

    private final StoryPointFieldSupport storyPointFieldSupport;

    private String issueSummary;

    private Double issueStoryPoints;

    private String issueKey;

    private String issueProjectName;

    private String issueProjectKey;

    private String issueReturnUrl;

    private Map<String, String> cardsForIssue;

    private Map<String, PokerCard> cardDeck = new HashMap<String, PokerCard>();
    
    private String chosenCard;

    private FieldLayoutManager fieldLayoutManager;

    private RendererManager rendererManager;

    private UserManager userManager;
    
    @HtmlSafe
    public String getIssueDescription() {
        MutableIssue issue = issueManager.getIssueObject(issueKey);
        FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
        FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(IssueFieldConstants.DESCRIPTION);
        String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        return rendererManager.getRenderedContent(rendererType, issue.getDescription(), issue.getIssueRenderContext());
    }

    public Double getIssueStoryPoints() {
        return issueStoryPoints;
    }

    public String getIssueProjectKey() {
        return issueProjectKey;
    }

    public String getIssueReturnUrl() {
        return issueReturnUrl;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public String getIssueProjectName() {
        return issueProjectName;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public boolean isDeckVisible() {
        return planningPokerStorage.isVisible(issueKey);
    }

    private PokerCard[] cards = {new PokerCard("q", "q.jpg"),
            new PokerCard("0", "0.jpg"),
            new PokerCard("0.5", "05.jpg"),
            new PokerCard("1", "1.jpg"),
            new PokerCard("2", "2.jpg"),
            new PokerCard("3", "3.jpg"),
            new PokerCard("5", "5.jpg"),
            new PokerCard("8", "8.jpg"),
            new PokerCard("13", "13.jpg"),
            new PokerCard("20", "20.jpg"),
            new PokerCard("40", "40.jpg"),
            new PokerCard("100", "100.jpg")};

    public Map<String, PokerCard> getCardDeck() {
        return cardDeck;
    }

    public String getChosenCard() {
        return chosenCard;
    }

    public PokerCard[] getCards() {
        return cards;
    }

    public StartPlanningPoker(IssueManager issueManager, PlanningPokerStorage planningPokerStorage, StoryPointFieldSupport storyPointFieldSupport, UserManager userManager) {
        this.issueManager = issueManager;
        this.planningPokerStorage = planningPokerStorage;
        this.storyPointFieldSupport = storyPointFieldSupport;
        this.userManager = userManager;

        fieldLayoutManager = ComponentAccessor.getComponent(FieldLayoutManager.class);
        rendererManager = ComponentAccessor.getComponent(RendererManager.class);

        for (PokerCard card : cards) {
            cardDeck.put(card.getName(), card);
        }
    }

    @Override
    protected String doExecute() throws Exception {
        issueKey = getHttpRequest().getParameter(PARAM_ISSUE_KEY);

        if (getLoggedInApplicationUser() == null) {
            return "error";
        }

        MutableIssue issue = issueManager.getIssueObject(issueKey);
        if (issue == null) {
            addErrorMessage("Issue Key" + issueKey + " not found.");
            return "error";
        }

        // weird hack to check whether we have been called from "outside"
        Boolean outsideCall = true;
        URL referrerURL = new URL(getHttpRequest().getHeader(PARAM_REFERRER_HEADER));
        String selfAction = getActionName().toLowerCase();
        String referrerPath = referrerURL.getPath().toLowerCase();
        String regex = ".*/" + selfAction + "\\.?\\w*";
        if (referrerPath.matches(regex)) {
            outsideCall = false;
        }

        // remember the page we have to return to after finishing the poker round
        String sessionUrl = (String)getHttpSession().getAttribute(PARAM_RETURN_URL);
        issueReturnUrl = getReturnUrl();
        if (sessionUrl == null || outsideCall) {
            if (issueReturnUrl == null) {
                issueReturnUrl = "/browse/" + issueKey;
            }
            getHttpSession().setAttribute(PARAM_RETURN_URL, issueReturnUrl);
        } else {
            issueReturnUrl = sessionUrl;
        }

        cardsForIssue = planningPokerStorage.chosenCardsForIssue(issueKey);
        chosenCard = cardsForIssue.get(getLoggedInApplicationUser().getKey());

        issueSummary = issue.getSummary();
        issueProjectName = issue.getProjectObject().getName();
        issueProjectKey = issue.getProjectObject().getKey();
        issueStoryPoints = storyPointFieldSupport.getValue(issueKey);

        return "start";
    }

    private Set<Integer> getSortedVotes(Map<String, String> votes) {
        Collection<String> votedValues = votes.values();
        Set<Integer> uniqueValues = new TreeSet<Integer>();
        for (String value : votedValues) {
            if (!value.equals("q")) {
                uniqueValues.add(new Integer(value));
            }
        }
        return uniqueValues;
    }

    public Collection<String> getBoundedVotes() {
        ArrayList<String> votes = new ArrayList<String>();
        for (Integer value : getSortedVotes(cardsForIssue)) {
            votes.add(value.toString());
        }
        ArrayList<String> boundedVotes = new ArrayList<String>();

        String first = votes.get(0);
        String last = votes.get(votes.size() - 1);

        if (votes.size() > 0) {

            int index = 0;
            while (!cards[index].getName().equals(first)) {
                index++;
            }
            boundedVotes.add(cards[index].getName());
            if (votes.size() > 1) {
                index++;
                while (!cards[index].getName().equals(last)) {
                    boundedVotes.add(cards[index].getName());
                    index++;
                }
                boundedVotes.add(cards[index].getName());
            }
        }
        return boundedVotes;
    }

    public String getMinVoted() {
        double min = 1000.0;
        for (String voted : getCardsForIssue().values()) {
            min = Math.min(new Double(min), new BigDecimal(voted).doubleValue());
        }
        return String.valueOf(min).replace(".0", "");
    }

    public String getMaxVoted() {
        double max = 0;
        for (String voted : getCardsForIssue().values()) {
            max = Math.max(new Double(max), new BigDecimal(voted).doubleValue());
        }
        return String.valueOf(max).replace(".0", "");
    }

    public Map<String, String> getCardsForIssue() {
        return cardsForIssue;
    }

    public String getUsername(String key) {
        return userManager.getUserByKey(key).getDisplayName();
    }

}
