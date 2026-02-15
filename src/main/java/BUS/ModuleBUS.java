package BUS;

import DAL.ModuleDAL;
import DTO.ModuleDTO;

import java.util.ArrayList;

public class ModuleBUS extends BaseBUS<ModuleDTO, Integer> {
    private static final ModuleBUS INSTANCE = new ModuleBUS();
    private static final ArrayList<ModuleDTO> MODULE_CACHE = new ArrayList<>();

    private ModuleBUS() {
    }

    public static ModuleBUS getInstance() {
        return INSTANCE;
    }

    public void loadCache() {
        if (!MODULE_CACHE.isEmpty())
            MODULE_CACHE.clear();
        MODULE_CACHE.addAll(ModuleDAL.getInstance().getAll());
    }

    @Override
    public ArrayList<ModuleDTO> getAll() {
        return MODULE_CACHE;
    }

    @Override
    protected Integer getKey(ModuleDTO obj) {
        return obj.getId();
    }

    @Override
    public ModuleDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        if (MODULE_CACHE.isEmpty()) {
            loadCache();
        }
        return MODULE_CACHE.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }
}
