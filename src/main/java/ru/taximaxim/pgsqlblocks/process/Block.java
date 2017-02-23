package ru.taximaxim.pgsqlblocks.process;

public class Block {

    private final int blockingPid;
    private final String relation;
    private final String locktype;
    private final boolean granted;

    public Block(int blockingPid, String locktype, String relation, boolean granted) {
        this.blockingPid = blockingPid;
        this.locktype = locktype == null ? "" : locktype;
        this.relation = relation == null ? "" : relation;
        this.granted = granted;
    }

    public String getLocktype() {
        return locktype;
    }

    public String getRelation() {
        return relation;
    }

    public int getBlockingPid() {
        return blockingPid;
    }

    public boolean isGranted() {
        return granted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Block block = (Block) o;

        if (isGranted() ^ block.isGranted()) {
            return false;
        }

        if (getBlockingPid() != block.getBlockingPid()) {
            return false;
        }

        if (getRelation() != null ? !getRelation().equals(block.getRelation()) : block.getRelation() != null) {
            return false;
        }
        return getLocktype().equals(block.getLocktype());
    }

    @Override
    public int hashCode() {
        int result = getBlockingPid();
        result = 31 * result + (getRelation() != null ? getRelation().hashCode() : 0);
        result = 31 * result + getLocktype().hashCode();
        result = 31 * result + (isGranted() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockingPid=" + blockingPid +
                ", relation='" + relation + '\'' +
                ", locktype='" + locktype + '\'' +
                ", granted='" + granted + '\'' +
                '}';
    }

}
