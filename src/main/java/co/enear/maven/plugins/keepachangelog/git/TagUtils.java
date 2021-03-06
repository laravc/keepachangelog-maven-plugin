package co.enear.maven.plugins.keepachangelog.git;

import co.enear.maven.plugins.keepachangelog.utils.Range;
import co.enear.maven.plugins.keepachangelog.utils.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LsRemoteCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static co.enear.maven.plugins.keepachangelog.InitMojo.UNRELEASED_VERSION;

/**
 * Git utilities.
 */
public class TagUtils {

    public static final String VERSION_ID = "version";
    public static final String DEFAULT_TAG_FORMAT = "v${" + VERSION_ID + "}";

    public static final String HEAD = "HEAD";
    public static final String REFS_TAGS = "refs/tags/";

    private static String getTagName(Ref tagRef) {
        return tagRef.getName().replaceFirst(REFS_TAGS, StringUtils.EMPTY_STRING);
    }

    /**
     * Returns the tags of the given Git repository.
     *
     * @param url      the Git repository URL.
     * @param username the username to connect to the URL.
     * @param password the password to connect to the URL.
     * @return a list of tags.
     * @throws GitAPIException if an error occurs while calling a Git command
     */
    public static List<String> getTags(URL url, String username, String password)
            throws GitAPIException {
        LsRemoteCommand cmd = Git.lsRemoteRepository();
        cmd.setRemote(url.toString());
        if (username != null && password != null) {
            cmd.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(username, password)
            );
        }
        Collection<Ref> refs = cmd.call();
        return refs.stream()
                .filter(ref -> ref.getName().startsWith(REFS_TAGS))
                .map(tag -> getTagName(tag))
                .collect(Collectors.toList());
    }

    /**
     * Converts a version to a tag.
     *
     * @param version the version to be converted.
     * @return a tag.
     */
    public static String toTag(String tagFormat, String version) {
        if (version.equals(UNRELEASED_VERSION)) {
            return HEAD;
        } else {
            return StringUtils.replace(tagFormat, VERSION_ID, version);
        }
    }

    /**
     * Converts a version range to a tag range.
     *
     * @param tagFormat    the tag format.
     * @param versionRange the version range.
     * @return a tag range.
     */
    public static Range<String> toTagRange(String tagFormat, Range<String> versionRange) {
        String b = toTag(tagFormat, versionRange.getBegin());
        String e = toTag(tagFormat, versionRange.getEnd());
        return new Range<>(b, e);
    }

    /**
     * Converts a tag to a version.
     *
     * @param tag the tag to be converted.
     */
    public static Optional<String> toVersion(String tagFormat, String tag) {
        return StringUtils.extract(tag, VERSION_ID, tagFormat);
    }
}
