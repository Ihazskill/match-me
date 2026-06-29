import { useEffect, useState } from "react";
import axios from "../api/axios";

export default function Connections() {
    const [connections, setConnections] = useState<any[]>([]);

    useEffect(() => {
        load();
    }, []);

    const load = async () => {
        const res = await axios.get("/connections");

        const full = await Promise.all(
            res.data.map(async (c: any) => {
                const user = await axios.get(`/users/${c.id}`);
                return user.data;
            })
        );

        setConnections(full);
    };

    const disconnect = async (id: number) => {
        await axios.post(`/connections/${id}/disconnect`);
        setConnections((prev) =>
            prev.filter((u) => u.id !== id)
        );
    };

    return (
        <div>
            <h1>Connections</h1>

            {connections.map((u) => (
                <div key={u.id}>
                    <h3>{u.name}</h3>

                    <button onClick={() => disconnect(u.id)}>
                        Disconnect
                    </button>
                </div>
            ))}
        </div>
    );
}