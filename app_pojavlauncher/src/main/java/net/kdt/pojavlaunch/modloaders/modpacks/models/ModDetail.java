package net.kdt.pojavlaunch.modloaders.modpacks.models;


import androidx.annotation.NonNull;

import java.util.Arrays;

public class ModDetail extends ModItem {
    
    public String[] versionNames;
    public String [] mcVersionNames;
    public String[] versionUrls;
    
    public String[] versionHashes;
    public String[] versionIds;
    public Dependencies[][] dependencies;
    public ModDetail(ModItem item, String[] versionNames, String[] versionIds, String[] mcVersionNames, String[] versionUrls, String[] hashes, Dependencies[][] dependencies) {
        super(item.apiSource, item.isModpack, item.id, item.title, item.description, item.imageUrl);
        this.versionNames = versionNames;
        this.mcVersionNames = mcVersionNames;
        this.versionIds = versionIds;
        this.versionUrls = versionUrls;
        this.versionHashes = hashes;
        this.dependencies = dependencies;

        
        for (int i=0; i<versionNames.length; i++){
            if (!versionNames[i].contains(mcVersionNames[i]))
                versionNames[i] += " - " + mcVersionNames[i];
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "ModDetail{" +
                "versionNames=" + Arrays.toString(versionNames) +
                ", mcVersionNames=" + Arrays.toString(mcVersionNames) +
                ", versionIds=" + Arrays.toString(versionIds) +
                ", versionUrls=" + Arrays.toString(versionUrls) +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", apiSource=" + apiSource +
                ", isModpack=" + isModpack +
                '}';
    }
    public static class Dependencies{
        public String project_id; 
        public String version_id;
        public String file_name;
        public String dependency_type;
        public Dependencies(String project_id, String version_id, String file_name, String dependency_type){
            this.project_id = project_id;
            this.version_id = version_id;
            this.file_name = file_name;
            this.dependency_type = dependency_type;
        }
    }

}
