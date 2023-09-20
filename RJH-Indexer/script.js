
window.onload = ()=>{
    getIndexes();
}
function getIndexes(){
    const baseURL = "http://localhost:8082/RJH/GetCores"; 
    let url = `http://localhost:8080/RJH/GetCores`
    fetch(url, {
      

    })
    .then((res)=>res.json())
    .then((result)=>{
      let dropDown = document.getElementById("myDropdown");
      let html = '';
      for(let res of result){
        html += `<option value=${res}>${res}</option>`;
      }
      dropDown.innerHTML = html;
    })
    .catch((err)=>{
        console.log(err)
    })
}
function createIndex(){
    const baseURL = "http://localhost:8080/RJH/GetCores";
    let indexVal = document.getElementById("index_id").value;
    let url = `http://localhost:8080/RJH/createIndex?indexName=${indexVal}`
    fetch(url, {
        headers: {
            "Content-Type": "application/json",
        },
        method:"POST"

    })
    .then((res)=>res.json())
    .then((result)=>{
        console.log(result);
        alert("success created!");
        let cusPopup = document.getElementById("cus_poupup");
        cusPopup.style.display = "none";
    })
    .catch((err)=>{
        console.log(err)
    })
}

function openPopup(){
    let cusPopup = document.getElementById("cus_poupup");
    cusPopup.style.display = "flex";
}
function stopPop(e){
    e.preventDefault();
    e.stopPropagation();
}
function search(){
    let index=document.getElementById("myDropdown").value
    let query=document.getElementById("query").value
    window.location.href=`/search.html?index=${index}&query=${query}`
}
function index(){
let index=document.getElementById("myDropdown").value
let uri="https://emlgbrab400.enron.hcp-demo.hcpdemo.com/rest/Test/"
let tenant="enron"
let hcp_system_name="hcp-demo.hcpdemo.com";
let url = `http://localhost:8080/RJH/IndexAndUpdatedata?loc=${uri}&tenant=${tenant}&hcp_system_name=${hcp_system_name}&index=${index}`
    fetch(url, {
        headers: {
            "Content-Type": "application/json",
        },
        method:"POST"

    })
    // .then((res)=>res.json())
    .then((result)=>{
        // console.log(result);
        if(result.status==200){
        alert("Indexed successfully Happy Searching");}
        else{
            alert("something went wrong")
        }
    })
        .catch((err)=>{
            console.log(err)
        })

}