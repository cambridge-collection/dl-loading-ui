package uk.cam.lib.cdl.loading.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.cam.lib.cdl.loading.model.logs.LogMessage;
import uk.cam.lib.cdl.loading.model.logs.LogType;

import java.util.List;

@Repository
public interface LogMessageRepository extends CrudRepository<LogMessage, Long> {

    LogMessage findByMessageId(String message_id);
    List<LogMessage> findTop20ByLogTypeOrderByTimestampDesc(LogType log_type);
    List<LogMessage> findTop20ByOrderByTimestampDesc();

}
