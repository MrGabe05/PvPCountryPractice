package com.gabrielhd.practice.utils.maps;

public interface TtlHandler<E>
{
    void onExpire(final E p0);
    
    long getTimestamp(final E p0);
}
