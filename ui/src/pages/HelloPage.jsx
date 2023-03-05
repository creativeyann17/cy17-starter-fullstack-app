import React from "react";
import useAxios from "axios-hooks";

const HelloPage = () => {
  const [{ data, loading, error }] = useAxios("/api/hello");
  return (
    <div className="page">
      <h1>{loading ? "loading..." : error ? error.message : data}</h1>
    </div>
  );
};

export default HelloPage;
