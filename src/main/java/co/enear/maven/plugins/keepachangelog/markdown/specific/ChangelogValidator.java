package co.enear.maven.plugins.keepachangelog.markdown.specific;

import co.enear.maven.plugins.keepachangelog.markdown.generic.RefLink;
import org.eclipse.jgit.api.errors.GitAPIException;
import co.enear.maven.plugins.keepachangelog.git.TagUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static co.enear.maven.plugins.keepachangelog.InitMojo.UNRELEASED_VERSION;

public class ChangelogValidator extends ChangelogReader {

    private Set<String> parsedVersions = new HashSet<>();
    private Set<String> versionsWithoutTags = Collections.EMPTY_SET;
    private Set<String> tagsWithoutVersions = Collections.EMPTY_SET;

    private URL url;
    private String username;
    private String password;
    private String tagFormat;

    public ChangelogValidator(URL url, String username, String password, String tagFormat) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.tagFormat = tagFormat;
    }

    @Override
    protected void onVersionHeading(VersionHeading versionHeading) {
        String version = versionHeading.getVersion();
        parsedVersions.add(version);
    }

    @Override
    protected void onRefLink(RefLink refLink) {
    }

    @Override
    protected void onOther(String line) {
    }

    @Override
    public void read(Path path) {
        super.read(path);
        try {
            Set<String> gitVersions = getTagsAsVersions();
            initVersionsWithoutTags(gitVersions);
            initTagsWithoutVersions(gitVersions);
        } catch (GitAPIException e) {
            throw new ReaderException("Failed to get tags as versions.", e);
        }
    }

    private Set<String> getTagsAsVersions() throws GitAPIException {
        List<String> tags = TagUtils.getTags(url, username, password);
        tags.remove(UNRELEASED_VERSION);
        return tags.stream().map(tag ->
                TagUtils.toVersion(tagFormat, tag)).
                filter(o -> o.isPresent()).
                map(o -> o.get()).
                collect(Collectors.toSet());
    }

    private void initTagsWithoutVersions(Set<String> gitVersions) {
        tagsWithoutVersions = new HashSet<>(gitVersions);
        tagsWithoutVersions.removeAll(parsedVersions);
    }

    private void initVersionsWithoutTags(Set<String> gitVersions) {
        versionsWithoutTags = new HashSet<>(parsedVersions);
        versionsWithoutTags.remove(UNRELEASED_VERSION);
        versionsWithoutTags.removeAll(gitVersions);
    }

    public Set<String> getVersionsWithoutTags() {
        return versionsWithoutTags;
    }

    public Set<String> getTagsWithoutVersions() {
        return tagsWithoutVersions;
    }
}
