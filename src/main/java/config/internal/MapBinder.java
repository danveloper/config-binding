package config.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBinder extends AbstractTypeBinder<Map> {
    @Override
    public Class<Map> getType() {
        return Map.class;
    }

    @Override
    public Class<LinkedHashMap> getImplType() {
        return LinkedHashMap.class;
    }
}
