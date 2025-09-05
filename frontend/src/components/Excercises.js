import { useEffect, useState } from "react";

function ExerciseList() {
    const [exercises, setExercises] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch("/exercises", {credentials: "include"})
            .then((res) => {
                if (!res.ok) {
                    throw new Error('HTTP error: status: ${res.status}');
                }
                return res.json();
            })
            .then((data) => {
                setExercises(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error("error fetching exercises: ", err);
                setError(err.message);
                setLoading(false);
            });
    }, []);
}