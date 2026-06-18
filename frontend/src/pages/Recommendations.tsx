import { useEffect, useState } from "react";
import axios from "../api/axios";

type Rec = { id: number };

export default function Recommendations() {
    const [recs, setRecs] = useState<Rec[]>([]);
    const [users, setUsers] = useState<any[]>([]);

    useEffect(() => {
        load();
    }, []);

    const load = async () => {
        const res = await axios.get("/recommendations");

        const limited = res.data.slice(0, 10);
        setRecs(limited);

        const full = await Promise.all(
            limited.map(async (r: Rec) => {
                const user = await axios.get(`/user/${r.id}`);
                return user.data;
            })
        );

        setUsers(full);
    };

    const connect = async (id: number) => {
        await axios.post(`/users/${id}/connect`);
        setUsers((prev) => prev.filter((u) => u.id !== id));
    };

    const dismiss = async (id: number) => {
        await axios.post(`/users/${id}/dismiss`);
        setUsers((prev) => prev.filter((u) => u.id !== id));
    };

    return (
        <div>
            <h1>Recommendations</h1>

            {users.map((u) => (
                <div key={u.id}>
                    <h3>{u.name}</h3>

                    {u.profileImageUrl && (
                        <img src={u.profileImageUrl} width={80} />
                    )}

                    <button onClick={() => connect(u.id)}>
                        Connect
                    </button>

                    <button onClick={() => dismiss(u.id)}>
                        Dismiss
                    </button>
                </div>
            ))}
        </div>
    );
}