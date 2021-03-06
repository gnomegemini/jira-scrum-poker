package de.codescape.jira.plugins.scrumpoker.upgrade;

import de.codescape.jira.plugins.scrumpoker.model.GlobalSettings;
import de.codescape.jira.plugins.scrumpoker.service.ScrumPokerSettingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Set the default setting for configuration option Display Comments for Issue.
 *
 * @since 4.6
 */
public class Upgrade07DisplayCommentsForIssue extends AbstractUpgradeTask {

    private final ScrumPokerSettingService scrumPokerSettingService;

    @Autowired
    public Upgrade07DisplayCommentsForIssue(ScrumPokerSettingService scrumPokerSettingService) {
        this.scrumPokerSettingService = scrumPokerSettingService;
    }

    @Override
    public int getBuildNumber() {
        return 7;
    }

    @Override
    public String getShortDescription() {
        return "Set default value for display comments for issue.";
    }

    @Override
    protected void performUpgrade() {
        GlobalSettings globalSettings = scrumPokerSettingService.load();
        globalSettings.setDisplayCommentsForIssue(GlobalSettings.DISPLAY_COMMENTS_FOR_ISSUE_DEFAULT);
        scrumPokerSettingService.persist(globalSettings);
    }

}
