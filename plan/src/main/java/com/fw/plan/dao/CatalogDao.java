package com.fw.plan.dao;

import com.fw.plan.PO.CatalogPO;
import com.mongodb.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CatalogDao {
    private static DBCollection mongoCollection;
    private static DBCollection documentMongoCollection;

    static {
        mongoCollection = new Mongo().getDB("plan").getCollection("catalog.db");
        documentMongoCollection = new Mongo().getDB("plan").getCollection("document.db");
    }

    public List<CatalogPO> getCatalogList() {
        DBCursor dbCursor = mongoCollection.find();
        List<CatalogPO> list = new ArrayList<>();
        while (dbCursor.hasNext()) {
            DBObject obj = dbCursor.next();
            list.add(convertToBean(obj));
        }

        System.out.println(list);
        return list;
    }

    public String initCatalog(List<CatalogPO> list) {
        mongoCollection.drop();

        List<BasicDBObject> bList = new ArrayList<>();

        for (CatalogPO catalog : list) {
            catalog.setModifierTime(dateStr());
            bList.add(convertToDBObject(catalog));
        }

        mongoCollection.insert(bList);

        return "初始化成功";
    }

    public String dateStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public String uuid() {
        return UUID.randomUUID().toString();
    }

    public String updateCatalogName(CatalogPO catalogPO) {
        if (catalogPO.getCatalogId() != null && !StringUtils.isEmpty(catalogPO.getTitle())) {
            BasicDBObject query = new BasicDBObject();
            query.put("catalogId", catalogPO.getCatalogId());

            BasicDBObject update = new BasicDBObject();
            update.put("title", catalogPO.getTitle());

            mongoCollection.update(query, new BasicDBObject("$set", update));

            return "更新成功";
        }

        return null;
    }

    /**
     * @param current  当前节点
     * @param old      上一次父节点
     * @param parent   当前父节点
     * @param position 相对于当前父节点位置
     * @return
     */
    public String updateCatalogOrder(CatalogPO current, CatalogPO old, CatalogPO parent, int position) {
        if (current == null || old == null || parent == null || position < 0)
            return null;

        // 检测 父子关系 是否成立(如果层级关系不成立，不应该继续)
        BasicDBObject query = new BasicDBObject();
        query.put("catalogId", current.getCatalogId());

        DBObject currentDBObject = mongoCollection.findOne(query);

        if (currentDBObject == null) {
            return "db中没有找到与当前数据id匹配的记录";
        }

        CatalogPO tempCurrent = convertToBean(currentDBObject);
        if (!tempCurrent.getParentId().equals(old.getCatalogId())) {
            return "根据current的catalogId获得的db中的parentId与old的Id不一致";
        }

        // 平级移动
        if (old.getCatalogId().equals(parent.getCatalogId())) {
            query.clear();
            query.put("catalogId", parent.getParentId());
            DBObject parentDBObject = mongoCollection.findOne(query);
            // parentDBObject 为空，表示父节点不存在
            // modifierTime 不相等，表示父节点被改动过（位置移动、子节点删除）
            if (parentDBObject == null) {
                return "新parent不存在";
            }

            CatalogPO tempParent = convertToBean(parentDBObject);
            if (!tempParent.getModifierTime().equals(parent.getModifierTime())) {
                return "新parent的modifierTime与db中的modifierTime不一致";
            }

            List<String> children = tempParent.getChildren();
            // children.size() 可能是0
            position = Math.min(Math.max(children.size() - 1, 0), position);
            // 先移除
            children.remove(current.getCatalogId());
            // 再添加到指定位置
            children.add(position, current.getCatalogId());

            BasicDBObject update = new BasicDBObject();
            update.put("children", children);
            update.put("modifierTime", dateStr());
            mongoCollection.update(query, new BasicDBObject("$set", update));
        }
        // 非平级移动
        else {
            query.clear();
            query.put("catalogId", parent.getParentId());
            DBObject parentDBObject = mongoCollection.findOne(query);
            // parentDBObject 为空，表示父节点不存在
            // modifierTime 不相等，表示父节点被改动过（位置移动、子节点删除）
            if (parentDBObject == null) {
                return "新parent不存在";
            }

            CatalogPO tempParent = convertToBean(parentDBObject);
            if (!tempParent.getModifierTime().equals(parent.getModifierTime())) {
                return "新parent的modifierTime与db中的modifierTime不一致";
            }

            List<String> children = tempParent.getChildren();

            position = Math.min(Math.max(children.size() - 1, 0), position);
            children.add(position, current.getCatalogId());

            BasicDBObject update = new BasicDBObject();
            update.put("children", children);
            update.put("modifierTime", dateStr());
            mongoCollection.update(query, new BasicDBObject("$set", update));

            query.clear();
            query.put("catalogId", old.getCatalogId());
            DBObject oldDBObject = mongoCollection.findOne(query);
            old = convertToBean(oldDBObject);
            List<String> oldChildren = old.getChildren();
            oldChildren.remove(current.getCatalogId());

            update.clear();
            update.put("children", oldChildren);
            update.put("modifierTime", dateStr());
            mongoCollection.update(query, new BasicDBObject("$set", update));
        }

        return "更新成功";
    }

    public String deleteCatalog(CatalogPO catalogPO) {
        BasicDBObject query = new BasicDBObject();
        // 保证当前catalogPO与数据库记录一致
        query.put("catalogId", catalogPO.getCatalogId());
        query.put("parentId", catalogPO.getParentId());
        query.put("modifierTime", catalogPO.getModifierTime());
        DBObject current = mongoCollection.findOne(query);

        // 只要modifierTime时间不一致，都不允许删除
        if (current == null)
            return null;

        // 获取parent记录
        query.clear();
        query.put("catalogId", catalogPO.getParentId());
        DBObject parent = mongoCollection.findOne(query);
        CatalogPO parentCatalog = convertToBean(parent);

        // parent 存在
        if (parent != null) {
            List<String> children = parentCatalog.getChildren();
            // 更新父节点 children
            children.remove(catalogPO.getCatalogId());
            BasicDBObject update = new BasicDBObject();
            update.put("children", children);
            update.put("modifierTime", dateStr());
            mongoCollection.update(query, new BasicDBObject("$set", update));

            // 获取需要删除的目录集合
            CatalogPO currentCatalog = convertToBean(current);
            List<String> currentChildren = currentCatalog.getChildren();

            List<String> ids = new ArrayList<>();

            ids.add(catalogPO.getCatalogId());
            ids.addAll(getIds(currentChildren));

            System.out.println(ids);

            // 删除目录
            query.clear();
            query.put("catalogId", new BasicDBObject("$in", ids));
            mongoCollection.remove(query);

            // 删除文档
            documentMongoCollection.remove(query);

            return "删除成功";
        }

        return null;
    }

    public List<String> getIds(List<String> list) {
        List<String> temp = new ArrayList<>();
        if (list.size() <= 0)
            return temp;

        BasicDBObject query = new BasicDBObject();
        query.put("catalogId", new BasicDBObject("$in", list));

        DBCursor dbCursor = mongoCollection.find(query);

        while (dbCursor.hasNext()) {
            DBObject next = dbCursor.next();
            CatalogPO catalogPO = convertToBean(next);
            List<String> children = catalogPO.getChildren();

            if (children.size() > 0) {
                temp.addAll(getIds(children));
            }
        }

        return temp;
    }

    public CatalogPO addCatalog(CatalogPO catalogPO) {
        catalogPO.setModifierTime(dateStr());
        catalogPO.setCatalogId(uuid());

        BasicDBObject object = convertToDBObject(catalogPO);

        if (catalogPO.getParentId() != null) {
            BasicDBObject query = new BasicDBObject();
            query.put("catalogId", catalogPO.getParentId());
            DBObject parent = mongoCollection.findOne(query);

            // 只要parent存在就允许插入
            if (parent != null) {
                CatalogPO parentCatalog = convertToBean(parent);
                // 不是directory
                if (!parentCatalog.isDir())
                    return null;

                mongoCollection.save(object);

                // 查询当前插入的记录
                CatalogPO currentCatalog = getByCatalogId(catalogPO.getCatalogId());

                // 更新父级children
                List<String> children = parentCatalog.getChildren();
                children.add(currentCatalog.getCatalogId());

                query.clear();
                query.put("catalogId", parentCatalog.getCatalogId());

                BasicDBObject update = new BasicDBObject();
                update.put("children", children);

                mongoCollection.update(query, new BasicDBObject("$set", update));

                return currentCatalog;
            }
        } else {
            // 仅 root = true 时执行， root的parentId为空
            mongoCollection.save(convertToDBObject(catalogPO));
            return getByCatalogId(catalogPO.getCatalogId());
        }

        return null;
    }

    public CatalogPO getByCatalogId(String id) {
        BasicDBObject query = new BasicDBObject();
        query.put("catalogId", id);
        DBObject dbObject = mongoCollection.findOne(query);

        if (dbObject != null)
            return convertToBean(dbObject);

        return null;
    }

    public BasicDBObject convertToDBObject(CatalogPO catalog) {
        BasicDBObject basicDBObject = new BasicDBObject();

        basicDBObject.put("catalogId", catalog.getCatalogId());
        basicDBObject.put("title", catalog.getTitle());
        basicDBObject.put("content", catalog.getContent());
        basicDBObject.put("children", catalog.getChildren());
        basicDBObject.put("parentId", catalog.getParentId());
        basicDBObject.put("root", catalog.isRoot());
        basicDBObject.put("dir", catalog.isDir());
        basicDBObject.put("modifier", catalog.getModifier());
        basicDBObject.put("modifierTime", catalog.getModifierTime());

        return basicDBObject;
    }

    public CatalogPO convertToBean(DBObject dbObject) {
        CatalogPO catalogPO = new CatalogPO();
        catalogPO.setCatalogId(Optional.ofNullable(dbObject.get("catalogId")).orElse("").toString());
        catalogPO.setTitle(Optional.ofNullable(dbObject.get("title")).orElse("").toString());
        catalogPO.setContent(Optional.ofNullable(dbObject.get("content")).orElse("").toString());
        catalogPO.setChildren((List<String>) Optional.ofNullable(dbObject.get("children")).orElse(new ArrayList<>()));
        catalogPO.setParentId(Optional.ofNullable(dbObject.get("parentId")).orElse("").toString());
        catalogPO.setRoot((Boolean) Optional.ofNullable(dbObject.get("root")).orElse(false));
        catalogPO.setDir((Boolean) Optional.ofNullable(dbObject.get("dir")).orElse(false));
        catalogPO.setModifier(Optional.ofNullable(dbObject.get("modifier")).orElse("").toString());
        catalogPO.setModifierTime(Optional.ofNullable(dbObject.get("modifierTime")).orElse("").toString());
        return catalogPO;
    }

}
