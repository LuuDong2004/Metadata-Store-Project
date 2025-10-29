package  com.vn.ms.interfaces;
import io.jmix.core.LoadContext;

import java.util.List;

public interface VirtualEntityHandler<Object> {
    List<Object> loadAll(LoadContext<?> ctx);
    Object loadOne(LoadContext<?> ctx);
}