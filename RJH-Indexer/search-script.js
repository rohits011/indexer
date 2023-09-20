window.onload = ()=>{
    getData();
}
function getData(){
    const urlParams = new URLSearchParams(window.location.search);
    const index = urlParams.get('index')
    const query = urlParams.get('query')
    console.log(index,query)
    let url = `http://localhost:8080/RJH/GetIndexData?query=${query}&field=*&index=${index}`
    fetch(url, {
      

    })
    .then((res)=>res.json())
    .then((result)=>{
        console.log(result);
        console.log(document.getElementById("indexName").value)
        document.getElementById("indexName").value=index;
        console.log(document.getElementById("indexName").value)
        document.getElementById("query").value=query;
        let docs= document.getElementById("documents");
        let html='';
        let j=0;
        for(let res of result){

            html+=`<button onclick="toggleDetails('details${j}')" class="document"id="document">${res["urlName"]}</button>`
            html+=` <div id="details${j}" class="details" >
            <table>
              <thead>
                <tr>
                  <th>Property</th>
                  <th>Detail</th>
                </tr>
              </thead>
              <tbody>
                `
                for(let i of Object.keys(res)){
                    let key=i;
                    let value=res[i];
                    html+=`<tr><td>${key}</td>
                    <td>${value}</td></tr>`
                }
                html+= `
                </tbody>
              </table>
            </div>`
                
          j++;        
        }
        docs.innerHTML=html;
    //   let dropDown = document.getElementById("myDropdown");
    //   let html = '';
    //   for(let res of result){
    //     html += `<option value=${res}>${res}</option>`;
    //   }
    //   dropDown.innerHTML = html;
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
function deleteData(){
    query=document.getElementById("index_id").value
    indexName=document.getElementById("indexName").value
    let url = `http://localhost:8080/RJH/DeleteIndexdatabyQuery?query=${query}&index=${indexName}`
    fetch(url, {
        headers: {
            "Content-Type": "application/json",
        },
        method:"DELETE"
    })
    // .then((res)=>res)
    .then((result)=>{
        console.log(result)
        alert("Deleted Successfully")
        let cusPopup = document.getElementById("cus_poupup");
        cusPopup.style.display = "none";
    })
    .catch((err)=>{
        console.log(err)
    })
}