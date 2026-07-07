import { useEffect, useState } from "react";
import axios from "../api/axios";

export default function ConnectionRequests() {
    const [requests, setRequests] = useState<any[]>([]);

    useEffect(() => {
        load();
    }, []);

    const load = async () => {
        const res = await axios.get("/connections/pending");
        setRequests(res.data);
    };

    const accept = async (id: number) => {
        await axios.post(`/connections/${id}/accept`);
        setRequests((r) => r.filter((x) => x.id !== id));
    };

    const reject = async (id: number) => {
        await axios.post(`/connections/${id}/reject`);
        setRequests((r) => r.filter((x) => x.id !== id));
    };

    return (
        <div>
            <h1>Connection Requests</h1>

            {requests.map((r) => (
                <div key={r.id}>
                    <p>Request #{r.id}</p>

                    <button onClick={() => accept(r.id)}>
                        Accept
                    </button>

                    <button onClick={() => reject(r.id)}>
                        Reject
                    </button>
                </div>
            ))}
        </div>
    );
}