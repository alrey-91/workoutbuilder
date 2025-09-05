import logo from './logo.svg';
import './App.css';
import { useEffect, useState } from 'react';

function App() {

  const [data, setData] = useState(null);

  useEffect(() => {
    fetch('/workouts', { credentials: 'include'})
    .then(res => res.json())
    .then(json => setData(json))
    .catch(err => console.error('Error fetching workouts (reactapp):', err));
  }, []);
  
  return (
    
  );
}

export default App;
