package tools.dynamia.modules.entityfile;

import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;


public interface EntityFileCache {
    Optional<Resource> get(String uuid, String etag);

    Resource put(String uuid, String etag, Resource resource);

    Path resolvePath(String uuid, String etag);

    void evict();

    Duration resolveTtl();
}
