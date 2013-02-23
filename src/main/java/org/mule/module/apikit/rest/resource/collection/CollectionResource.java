
package org.mule.module.apikit.rest.resource.collection;

import org.mule.module.apikit.rest.operation.RestOperationType;
import org.mule.module.apikit.rest.resource.AbstractHierarchicalRestResource;

import java.util.EnumSet;
import java.util.Set;

public class CollectionResource extends AbstractHierarchicalRestResource
{

    protected CollectionMemberResource memberResource;

    public CollectionResource(String name)
    {
        super(name);
    }

    @Override
    protected Set<RestOperationType> getSupportedActionTypes()
    {
        return EnumSet.of(RestOperationType.RETRIEVE, RestOperationType.EXISTS, RestOperationType.CREATE);

    }

    public void setMemberResource(CollectionMemberResource memberResource)
    {
        this.memberResource = memberResource;
    }

    public CollectionMemberResource getMemberResource()
    {
        return memberResource;
    }

}
